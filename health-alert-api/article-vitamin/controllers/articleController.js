const pool = require('../../db/db');

exports.getAllArticles = async (request, h) => {
    try {
        const [articles] = await pool.query('SELECT * FROM article ORDER BY created_at DESC');

        if (articles.length === 0) {
            return h.response({error: 'No articles found'}).code(404);
        }

        return h.response({articles}).code(200);
    } catch (error) {
        console.error('Error in getAllArticles Handler:', error.message, error.stack);
        return h.response({error: 'Internal Server Error'}).code(500);
    }
};