INSERT INTO PUBLIC.GENRE (name)
SELECT t.* FROM (
                    VALUES
                        ('Комедия'),
                        ('Драма'),
                        ('Мультфильм'),
                        ('Триллер'),
                        ('Документальный'),
                        ('Боевик')
                ) AS t(gname)
WHERE NOT EXISTS (SELECT 1 FROM PUBLIC.GENRE WHERE name = t.gname);

INSERT INTO PUBLIC.rating (name)
SELECT t.* FROM (
                    VALUES
                        ('G'),
                        ('PG'),
                        ('PG-13'),
                        ('R'),
                        ('NC-17')
                ) AS t(mpaname)
WHERE NOT EXISTS (SELECT 1 FROM PUBLIC.rating WHERE name = t.mpaname);