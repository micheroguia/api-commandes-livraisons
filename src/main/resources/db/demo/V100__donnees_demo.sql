-- Produits d'exemple, chargés uniquement avec le profil « demo ».
-- Les prix sont fictifs : ils servent seulement à essayer l'API.
insert into produit (nom, description, prix_fcfa, stock, actif, cree_le, version) values
    ('Pack eau minérale 1,5 L (x6)', 'Produit de démonstration', 2500, 40, true, current_timestamp, 0),
    ('Recharge gaz 12,5 kg', 'Produit de démonstration', 6500, 15, true, current_timestamp, 0),
    ('Sac de riz 25 kg', 'Produit de démonstration', 18000, 10, true, current_timestamp, 0),
    ('Huile raffinée 1 L', 'Produit de démonstration', 1200, 30, true, current_timestamp, 0);
