const pool = require('../../db/db');
require('dotenv').config();

exports.addHeartTest = async (request, h) => {
    const { exerciseHeartRate, testDate, testTime, result } = request.payload;
    const userId = request.user.userId;

    if (!exerciseHeartRate || !testDate || !testTime || result === undefined) {
        return h.response({ error: 'All fields are required' }).code(400);
    }

    const indicator = result >=0.5 ? 'Beresiko' : 'Sehat';

    try {
        await pool.query(
            'INSERT INTO user_heart_tests (user_id, exercise_heart_rate, test_date, test_time, result, indicator) VALUES (?, ?, ?, ?, ?, ?)',
            [userId, exerciseHeartRate, testDate, testTime, result, indicator]
        );

        return h.response({ message: 'Heart test added successfully' }).code(201);
    } catch (error) {
        console.error('Error in addHeartTest Handler:', error.message, error.stack);
        return h.response({ error: 'Internal Server Error' }).code(500);
    }
};

exports.getHeartTests = async (request, h) => {
    const userId = request.user.userId;

    try {
        const [testResults] = await pool.query('SELECT * FROM user_heart_tests WHERE user_id = ? ORDER BY test_date DESC, test_time DESC', [userId]);
        console.log('Query Result:', testResults);

        if (testResults.length === 0) {
            return h.response({ error: 'No heart test results found' }).code(404);
        }

        return h.response({ heartTests: testResults }).code(200);
    } catch (error) {
        console.error('Error in getHeartTests Handler:', error.message, error.stack);
        return h.response({ error: 'Internal Server Error' }).code(500);
    }
};