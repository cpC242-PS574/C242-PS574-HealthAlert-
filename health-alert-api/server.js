const Hapi = require('@hapi/hapi');
const userRoutes = require('./user/routes/userRoutes');
const heartRoutes = require('./user/routes/heartRoutes');
const articleRoutes = require('./article-vitamin/routes/articleRoutes');
const vitaminRoutes = require('./article-vitamin/routes/vitaminRoutes');
const hospitalRoutes = require('./user/routes/hospitalRoutes');
const Inert = require('@hapi/inert');
const cron = require('node-cron');
const pool = require('./db/db');
require('dotenv').config();

const init = async () => {
    const server = Hapi.server({
        port: 3000,
        host: 'localhost',
    });

    await server.register(Inert);

    server.route([
        ...userRoutes, 
        ...heartRoutes, 
        ...articleRoutes, 
        ...vitaminRoutes,
        ...hospitalRoutes
    ]);

    await server.start();
    console.log(`Server running on ${server.info.uri}`);
};

// Schedule job to run every 24 hour
cron.schedule('0 0 * * *', async () => {
    try {
        const query = `
            DELETE FROM users 
            WHERE is_verified = 0 
            AND created_at < NOW() - INTERVAL 24 HOUR
        `;
        const [result] = await pool.query(query);
        console.log(`Deleted ${result.affectedRows} unverified users`);
    } catch (error) {
        console.error('Error running scheduled job:', error.message, error.stack);
    }
});

process.on('unhandledRejection', (err) =>{
    console.error(err);
    process.exit(1);
});

init();