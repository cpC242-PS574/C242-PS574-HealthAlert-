const jwt = require('jsonwebtoken');
require('dotenv').config();

const verifyToken = (request, h) => {
    const authHeader = request.headers['authorization'];

    if(!authHeader){
        return h.response({error: 'Authorization header is missing'}).code(403);
    }

    const token = authHeader.split(' ')[1];

    if(!token){
        return h.response({error: 'Token is required'}).code(403);
    }

    try{
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        request.user = decoded;
        return h.continue;
    } catch (error) {
        console.error('Token verifications failed', error.message);
        return h.response({error: 'Invalid or expired token'}).code(403);
    }
};

module.exports = verifyToken;