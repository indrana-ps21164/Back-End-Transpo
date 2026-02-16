CREATE TABLE IF NOT EXISTS ticket_prices (
  id BIGSERIAL PRIMARY KEY,
  route_id BIGINT NOT NULL,
  from_stop_id BIGINT NOT NULL,
  to_stop_id BIGINT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ticket_price_route FOREIGN KEY (route_id) REFERENCES routes(id),
  CONSTRAINT fk_ticket_price_from_stop FOREIGN KEY (from_stop_id) REFERENCES stops(id),
  CONSTRAINT fk_ticket_price_to_stop FOREIGN KEY (to_stop_id) REFERENCES stops(id),
  CONSTRAINT chk_ticket_price_distinct CHECK (from_stop_id <> to_stop_id)
);

-- Unique on normalized pair (min(from,to), max(from,to)) per route to enforce symmetry
CREATE UNIQUE INDEX IF NOT EXISTS ux_ticket_price_route_pair
ON ticket_prices (route_id, LEAST(from_stop_id, to_stop_id), GREATEST(from_stop_id, to_stop_id));
