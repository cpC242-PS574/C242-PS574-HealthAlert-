const Hapi = require('@hapi/hapi');
const userRoutes = require('./user/routes/userRoutes');
const heartRoutes = require('./user/routes/heartRoutes');
require('dotenv').config();

const init = async () => {
    const server = Hapi.server({
        port: 3000,
        host: 'localhost',
    });

    server.route([...userRoutes, ...heartRoutes]);

    await server.start();
    console.log(`Server running on ${server.info.uri}`);
};

process.on('unhandledRejection', (err) =>{
    console.error(err);
    process.exit(1);
});

init();