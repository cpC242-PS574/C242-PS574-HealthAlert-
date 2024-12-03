const articleController = require('../controllers/articleController');

module.exports = [
    {
        method: 'GET',
        path: '/articles',
        handler: articleController.getAllArticles,
    },
];