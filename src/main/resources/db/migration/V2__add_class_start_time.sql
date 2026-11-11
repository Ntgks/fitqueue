ALTER TABLE gym_classes
ADD COLUMN starts_at TIMESTAMPTZ;

UPDATE gym_classes
SET starts_at = CURRENT_TIMESTAMP + INTERVAL '1 day';

ALTER TABLE gym_classes
ALTER COLUMN starts_at SET NOT NULL;