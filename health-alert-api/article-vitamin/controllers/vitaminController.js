const pool = require('../../db/db');

exports.getAllVitamins = async (request, h) => {
    try {
        const [vitamins] = await pool.query('SELECT * FROM vitamins');

        if (vitamins.length === 0) {
            return h.response({error: 'No vitamin found'}).code(404);
        }

        return h.response({vitamins}).code(200);
    } catch (error) {
        console.error('Error in getAllVitamins Handler:', error.message, error.stack);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};