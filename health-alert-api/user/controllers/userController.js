const pool = require('../../db/db');
const bcrypt = require('bcryptjs');
const sendOTP = require('../utils/email');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');
const { register } = require('module');
require('dotenv').config();

const generateToken = (userId) => {
    const payload = {userId};
    const secret = process.env.JWT_SECRET;
    const options = {expiresIn: '10h'};
    return jwt.sign(payload, secret, options);
};

const generateOTP = () => {
    return crypto.randomInt(100000, 999999).toString();
};

exports.registerUser = async (request, h) => {
    const {fullname, email, password} = request.payload;

    if(!email || !password){
        return h.response({ error: 'Full name, email and password are required'}).code(400);
    }

    try{
        //Hash password
        const hashedPassword = await bcrypt.hash(password, 10);
        const [existingUser] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
        if(existingUser.length > 0){
            return h.response({error: 'Email already registered'}).code(409);
        }

        const [result] = await pool.query('INSERT INTO users (fullname, email, password) VALUES (?, ?,?)', 
            [fullname, email, hashedPassword]);

        // Generate OTP dan pengaturan kedaluwarsa
        const otp = generateOTP();
        const expiresAt = new Date();
        expiresAt.setMinutes(expiresAt.getMinutes() + 5);

        await pool.query('INSERT INTO otp_codes (email, otp, expires_at) VALUES (?, ?, ?)', [email, otp, expiresAt]);

        await sendOTP(email, otp);

        return h.response({message: 'User registered. OTP sent to email for verification.'}).code(201);
    } catch (error){
        console.error(error);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};

exports.verifyOTP = async (request, h) => {
    const {email, otp} = request.payload;

    try{
        const [otpRecord] = await pool.query('SELECT * FROM otp_codes WHERE email = ? ORDER BY created_at DESC LIMIT 1', [email]);
        if (otpRecord.length === 0 || otpRecord[0].otp !== otp){
            return h.response({error: 'Invalid or expired OTP'}).code(400);
        }

        const now = new Date();
        if(new Date(otpRecord[0].expires_at) < now){
            await pool.query('DELETE FROM otp_codes WHERE email = ?', [email]);
            return h.response({error: 'OTP has expired'}).code(400);
        }

        await pool.query('UPDATE users SET is_verified = ? WHERE email = ?', [true, email]);

        await pool.query('DELETE FROM otp_codes WHERE email = ?', [email]);

        return h.response({message: 'Email verified successfully'}).code(200);
    } catch (error){
        console.error(error);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};

exports.loginUser = async (request, h) => {
    const {email, password} = request.payload;

    try{
        const [user] = await pool.query ('SELECT * FROM users WHERE email = ?', [email]);

        if (user.length === 0){
            return h.response({error: 'Invalid email or password'}).code(400);
        }

        if (!user[0].is_verified){
            return h.response({ error: 'Email not verified'}).code(400);
        }

        const validPassword = await bcrypt.compare(password, user[0].password);
        if (!validPassword){
            return h.response({error: 'Invalid password'}).code(400);
        }

        const token = generateToken(user[0].id);

        return h.response({message:'Login succesful', token}).code(200);
    } catch (error) {
        console.error(error);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};

exports.getProfile = async (request, h) => {
    const userId = request.user.userId;

    try{
        const [user] = await pool.query('SELECT * FROM users WHERE id = ?', [userId]);
        console.log('Query Result:', user);

        if (user.length === 0){
            return h.response({error: 'User not found'}).code(404);
        }

        return h.response({
            user: {
                id: user[0].id,
                fullname: user[0].fullname,
                email: user[0].email
            }
        })
    } catch (error) {
        console.error('Error in getProfile Handler:', error.message, error.stack);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};

exports.updatePassword = async (request, h) => {
    const {oldPassword, newPassword} = request.payload;
    const userId = request.user.userId;

    if (!oldPassword || !newPassword) {
        return h.response({ error: 'Both old and new passwords are required' }).code(400);
    }

    try {
        const [user] = await pool.query('SELECT * FROM users WHERE id = ?', [userId]);

        if(user.length === 0){
            return h.response({error: 'User not found'}).code(404);
        }

        const validPassword = await bcrypt.compare(oldPassword, user[0].password);
        if(!validPassword){
            return h.response({error: 'Incorrect old password'}).code(400);
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        await pool.query('UPDATE users SET password = ? WHERE id = ?', [hashedPassword, userId]);

        return h.response({message: 'Password updated successfully'}).code(200);
    } catch (error) {
        console.error(error);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};

exports.forgotPassword = async (request, h) => {
    const {email} = request.payload;

    if(!email) {
        return h.response({error: 'Email is required'}).code(400);
    }

    try{
        // Periksa apakah email terdaftar
        const [user] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
        if(user.length === 0) {
            return h.response({error: 'Email not found'}).code(404);
        }

        //OTP
        const otp = generateOTP();
        const expiresAt = new Date();
        expiresAt.setMinutes(expiresAt.getMinutes() + 5);

        await pool.query('INSERT INTO otp_codes (email, otp, expires_at) VALUES (?, ?, ?)', [email, otp, expiresAt]);

        //Kirim OTP
        await sendOTP(email, otp);

        return h.response({message: 'OTP sent to email for password reset'}).code(200);
    } catch (error) {
        console.error(error);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};

exports.resetPassword = async (request, h) => {
    const {email, otp, newPassword} = request.payload;

    if(!email || !otp || !newPassword) {
        return h.response({error: 'Email, OTP and new password are required'}).code(400);
    }

    try{
        //Periksa OTP di database
        const [otpRecord] = await pool.query('SELECT * FROM otp_codes WHERE email = ? ORDER BY created_at DESC LIMIT 1', [email]);

        if (otpRecord.length === 0 || otpRecord[0].otp !== otp) {
            return h.response({error: 'Invalid or expired OTP'}).code(400);
        }

        const now = new Date();
        if (new Date(otpRecord[0].expires_at) < now) {
            await pool.query('DELETE FROM otp_codes WHERE email = ?', [email]);
            return h.response({error: 'OTP has expired'}).code(400);
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        await pool.query('UPDATE users SET password = ? WHERE email = ?', [hashedPassword, email]);

        await pool.query('DELETE FROM otp_codes WHERE email = ?', [email]);

        return h.response({message: 'Password reset successfully'}).code(200);
    } catch (error) {
        console.error(error);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};

exports.addHeartTest = async (request, h) => {
    const { restHeartRate, exerciseHeartRate, testDate, testTime, result } = request.payload;
    const userId = request.user.userId;

    if (!restHeartRate || !exerciseHeartRate || !testDate || !testTime || !result) {
        return h.response({ error: 'All fields are required' }).code(400);
    }

    try {
        await pool.query(
            'INSERT INTO user_heart_tests (user_id, rest_heart_rate, exercise_heart_rate, test_date, test_time, result) VALUES (?, ?, ?, ?, ?, ?)',
            [userId, restHeartRate, exerciseHeartRate, testDate, testTime, result]
        );

        return h.response({ message: 'Heart test added successfully' }).code(201);
    } catch (error) {
        console.error('Error in addHeartTest Handler:', error.message, error.stack);
        return h.response({ error: 'Internal Server Error' }).code(500);
    }
};

exports.getHeartTests = async (request, h) => {
    const userId = request.user.userId;

    try {
        const [testResults] = await pool.query('SELECT * FROM user_heart_tests WHERE user_id = ? ORDER BY test_date DESC, test_time DESC', [userId]);
        console.log('Query Result:', testResults);

        if (testResults.length === 0) {
            return h.response({ error: 'No heart test results found' }).code(404);
        }

        return h.response({ heartTests: testResults }).code(200);
    } catch (error) {
        console.error('Error in getHeartTests Handler:', error.message, error.stack);
        return h.response({ error: 'Internal Server Error' }).code(500);
    }
};

