CREATE TABLE IF NOT EXISTS app_ratings (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  profile_image_url VARCHAR(255),
  rating INT NOT NULL,
  comment TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_rating_range CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS ix_app_ratings_username ON app_ratings(username);