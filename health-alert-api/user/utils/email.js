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

const sendEmail = async (email, subject, textContent) => {
    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: email,
        subject: subject,
        text: textContent,
    };

    try {
        await transporter.sendMail(mailOptions);
        console.log('Email sent to:', email);
    } catch (error) {
        console.error('Error sending email:', error);
    }
};

const sendVerificationEmail = async (email, otp, fullname) => {
    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Verifikasi Email Anda',
        html: `
            <p>Halo ${fullname},</p>
            
            <p>Terima kasih telah mendaftar di <strong>HealthAlert</strong>. Untuk menyelesaikan pendaftaran, silakan verifikasi email Anda dengan memasukkan kode OTP berikut:</p>
            
            <p><strong>OTP: ${otp}</strong></p>
            
            <p>Kode OTP ini berlaku selama 5 menit. Jika Anda tidak melakukan pendaftaran, abaikan email ini.</p>
            
            <p>Salam,<br>Tim HealthAlert</p>
        `,
    };

    try {
        await transporter.sendMail(mailOptions);
        console.log('OTP sent to:', email);
    } catch (error) {
        console.error('Error sending OTP:', error);
    }
};

const sendPasswordResetOTP = async (email, otp, fullname) => {
    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Verifikasi Email Anda',
        html: `
            <p>Halo ${fullname},</p>
            
            <p>Kami menerima permintaan untuk mereset kata sandi Anda di <strong>HealthAlert</strong>. Untuk melanjutkan, silakan masukkan kode OTP berikut:</p>
            
            <p><strong>OTP: ${otp}</strong></p>
            
            <p>Kode OTP ini berlaku selama 5 menit. Jika Anda tidak mengajukan permintaan ini, abaikan email ini.</p>
            
            <p>Salam,<br>Tim HealthAlert</p>
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
            <p>Halo ${fullname},</p>
            
            <p>Kata sandi akun Anda telah berhasil diubah. Jika Anda tidak melakukan perubahan ini, segera hubungi kami untuk melaporkan masalah ini.</p>
            
            <p>Salam,<br>Tim HealthAlert</p>
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