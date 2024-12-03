const axios = require('axios');
require('dotenv').config();

const getNearbyHospitals = async (latitude, longitude) => {
    const apiKey = process.env.GOOGLE_MAPS_API_KEY;
    const radius = 10000;
    const type = 'hospital';

    const url = `https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${latitude},${longitude}&radius=${radius}&type=${type}&key=${apiKey}`;
    const response = await axios.get(url);

    if(response.data.status !== 'OK') {
        throw new error('Failed to fetch hospitals');
    }

    return response.data.results;
};

const getTravelTimes = async (userLocation, hospitalLocation) => {
    const apiKey = process.env.GOOGLE_MAPS_API_KEY;

    const origins = `${userLocation.latitude},${userLocation.longitude}`;
    const destinations = hospitalLocation.map(loc => `${loc.lat},${loc.lng}`).join('|');

    const url = `https://maps.googleapis.com/maps/api/distancematrix/json?origins=${origins}&destinations=${destinations}&key=${apiKey}`;
    const response = await axios.get(url);

    if (response.data.status !== 'OK') {
        throw new Error('Failed to fetch travel times');
    }

    return response.data.rows[0].elements;
};

exports.getNearbyHospitalsHandler = async (request, h) => {
    const {latitude, longitude} = request.payload;

    if (!latitude || !longitude) {
        return h.response({error: 'Latitude and longitude are required'}).code(400);
    }

    try {
        const hospitals = await getNearbyHospitals(latitude, longitude);

        const hospitalLocation = hospitals.map(hospital => hospital.geometry.location);

        const travelTimes = await getTravelTimes({latitude, longitude}, hospitalLocation);

        const hospitalData = hospitals.map((hospital, index) => ({
            name: hospital.name,
            location: hospital.geometry.location,
            duration: travelTimes[index].duration?.text || 'N/A',
        }));

        return h.response({hospitals: hospitalData}).code(200);
    } catch (error) {
        console.error(error);
        return h.response({error: 'Internal server error'}).code(500);
    }
};