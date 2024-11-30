const userController = require ('../controllers/userController');
const verifyToken = require('../middleware/authMiddleware');
require('dotenv').config();

module.exports = [
    {
        method: 'POST',
        path: '/register',
        handler: userController.registerUser,
    },
    {
        method: 'POST',
        path: '/verify-otp',
        handler: userController.verifyOTP,
    },
    {
        method: 'POST',
        path: '/login',
        handler: userController.loginUser,
    },
    {
        method: 'GET',
        path: '/profile',
        handler: userController.getProfile,
        options: {
            pre: [{method: verifyToken, assign: 'auth'}],
        },
    },
    {
        method: 'PUT',
        path: '/update-password',
        handler: userController.updatePassword,
        options: {
            pre: [{method: verifyToken, assign: 'auth'}],
        },
    },
    {
        method: 'POST',
        path: '/forgot-password',
        handler: userController.forgotPassword,
    },
    {
        method: 'POST',
        path: '/reset-password',
        handler: userController.resetPassword,
    },
    {
        method: 'GET',
        path: '/heart-test',
        handler: userController.getHeartTests,
        options: {
            pre: [{method: verifyToken, assign: 'auth'}],
        },
    },
];