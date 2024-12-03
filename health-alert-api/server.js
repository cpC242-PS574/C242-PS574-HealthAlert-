const Hapi = require('@hapi/hapi');
const userRoutes = require('./user/routes/userRoutes');
const heartRoutes = require('./user/routes/heartRoutes');
const articleRoutes = require('./article-vitamin/routes/articleRoutes');
const vitaminRoutes = require('./article-vitamin/routes/vitaminRoutes');
const hospitalRoutes = require('./user/routes/hospitalRoutes');
require('dotenv').config();

const init = async () => {
    const server = Hapi.server({
        port: 3000,
        host: 'localhost',
    });

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

process.on('unhandledRejection', (err) =>{
    console.error(err);
    process.exit(1);
});

init();