const hospitalController = require('../controllers/hospitalController');

module.exports = [
    {
        method: 'POST',
        path: '/hospital',
        handler: hospitalController.getNearbyHospitalsHandler,
    },
];