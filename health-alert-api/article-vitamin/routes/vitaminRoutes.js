const vitaminController = require('../controllers/vitaminController');

module.exports = [
    {
        method: 'GET',
        path: '/vitamins',
        handler: vitaminController.getAllVitamins,
    },
];