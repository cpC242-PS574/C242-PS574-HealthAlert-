const nodemailer = require('nodemailer');
require('dotenv').config();

const transporter = nodemailer.createTransport({
    host: process.env.EMAIL_HOST,
    port: process.env.EMAIL_PORT,
    secure: true,
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS,
    },
});

const sendVerificationEmail = async (email, otp, fullname) => {
    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Verifikasi Email Anda',
        html: `
            <div style="background-color: #ffffff; font-family: Arial, sans-serif; padding: 20px; border-radius: 10px; border: 1px solid #f1f1f1;">
                <div style="text-align: center; margin-bottom: 20px;">
                   <h1 style="color: #FFD700; ">HealthAlert</h1>
                </div>
                <p style="font-size: 16px; color: #333;">Halo <strong>${fullname}</strong>,</p>
                
                <p style="font-size: 16px; color: #333;">Terima kasih telah mendaftar di <strong>HealthAlert</strong>. Untuk menyelesaikan pendaftaran, silakan verifikasi email Anda dengan memasukkan kode OTP berikut:</p>
                
                <div style="text-align: center">
                <p style="font-size: 18px; color: #333; background-color: #FFD700; padding: 10px; text-align: center; border-radius: 5px; display: inline-block;">
                    <strong>${otp}</strong>
                </p>
                </div>
                
                <p style="font-size: 14px; color: #666; margin-top: 20px;">Kode OTP ini berlaku selama 5 menit. Jika Anda tidak melakukan pendaftaran, abaikan email ini.</p>
                
                <p style="font-size: 14px; color: #333; margin-top: 30px;">Salam,<br><strong>Tim HealthAlert</strong></p>
            </div>
        `,
    };

    try {
        await transporter.sendMail(mailOptions);
        console.log('OTP sent to:', email);
    } catch (error) {
        console.error('Error sending OTP:', error);
    }
};

const sendPasswordResetOTP = async (email, otp, fullname, protocol, host) => {
        const mailOptions = {
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Reset Kata Sandi Anda',
        html: `
            <div style="background-color: #ffffff; font-family: Arial, sans-serif; padding: 20px; border-radius: 10px; border: 1px solid #f1f1f1;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <h1 style="color: #FFD700;">HealthAlert</h1>
                </div>
                <p style="font-size: 16px; color: #333;">Halo <strong>${fullname}</strong>,</p>
                
                <p style="font-size: 16px; color: #333;">Kami menerima permintaan untuk mereset kata sandi Anda di <strong>HealthAlert</strong>. Untuk melanjutkan, silakan klik tombol berikut:</p>

            <div style="text-align: center; margin: 5rem 0;">
                <p style="font-size: 18px; color: #333;">
                    <strong>${otp}</strong>
            <div>
                <a href="${protocol}://${host}/reset-password?email=${email}&otp=${otp}" 
                style="display: inline-block; margin-top: 2px; padding: 10px 20px; background-color: #FFD700; color: #333; text-decoration: none; font-size: 16px; border-radius: 5px; text-align: center;">
                Reset Kata Sandi
                </a>
            </div>
            </div>
                <p style="font-size: 14px; color: #666; margin-top: 20px;">Kode OTP ini berlaku selama 5 menit. Jika Anda tidak mengajukan permintaan ini, abaikan email ini.</p>
                
                <p style="font-size: 14px; color: #333; margin-top: 30px;">Salam,<br><strong>Tim HealthAlert</strong></p>
            </div>
        `,
    };

    try {
        await transporter.sendMail(mailOptions);
        console.log('Password reset OTP sent to:', email);
    } catch (error) {
        console.error('Error sending password reset OTP:', error);
    }
};


const sendPasswordResetSuccess = async (email, fullname) => {
    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Kata Sandi Anda Telah Diubah',
        html: `
            <div style="background-color: #ffffff; font-family: Arial, sans-serif; padding: 20px; border-radius: 10px; border: 1px solid #f1f1f1;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <h1 style="color: #FFD700;">HealthAlert</h1>
                </div>
                <p style="font-size: 16px; color: #333;">Halo <strong>${fullname}</strong>,</p>
                
                <p style="font-size: 16px; color: #333;">Kata sandi akun Anda telah berhasil diubah. Jika Anda tidak melakukan perubahan ini, segera hubungi kami untuk melaporkan masalah ini.</p>
                
                <p style="font-size: 14px; color: #333; margin-top: 30px;">Salam,<br><strong>Tim HealthAlert</strong></p>
            </div>
        `,
    };

    try {
        await transporter.sendMail(mailOptions);
        console.log('Password reset success email sent to:', email);
    } catch (error) {
        console.error('Error sending password reset success email:', error);
    }
};


module.exports = { sendVerificationEmail, sendPasswordResetOTP, sendPasswordResetSuccess };