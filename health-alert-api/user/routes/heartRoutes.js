const heartTestController = require('../controllers/heartController');
const verifyToken = require('../middleware/authMiddleware');

module.exports = [
    {
        method: 'POST',
        path: '/heart-test',
        handler: heartTestController.addHeartTest,
        options: {
            pre: [{method: verifyToken, assign: 'auth'}],
        },
    },
    {
        method: 'GET',
        path: '/heart-test',
        handler: heartTestController.getHeartTests,
        options: {
            pre: [{method: verifyToken, assign: 'auth'}],
        },
    },
];