-- Seed Categories (Exactly 9 specified categories)
MERGE INTO categories (category_id, category_name) KEY(category_id) VALUES 
(1, 'Plants'),
(2, 'Pots'),
(3, 'Soils'),
(4, 'Fertilisers'),
(5, 'Seeds'),
(6, 'Garden Tools'),
(7, 'Pest Control'),
(8, 'Gardening Decor'),
(9, 'Watering Solutions');
