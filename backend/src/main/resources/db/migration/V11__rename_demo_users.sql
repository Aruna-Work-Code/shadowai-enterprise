-- Rename local demo accounts only.
-- No governance workflow, permissions, or application behavior is changed.
UPDATE users
SET username = CASE username
    WHEN 'aruna.employee' THEN 'priya.employee'
    WHEN 'priya.it' THEN 'aruna.it'
    ELSE username
END
WHERE username IN ('aruna.employee', 'priya.it');
