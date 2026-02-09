import React, { useEffect, useState } from 'react';
import { getMyReservations } from '../api/resources';

const ReservationList = () => {
  const [items, setItems] = useState([]);

  useEffect(() => {
    (async () => {
      try {
        const data = await getMyReservations();
        setItems(data);
      } catch (e) {
        console.error(e);
      }
    })();
  }, []);

  return (
    <div>
      {/* ...existing JSX to render reservations using items... */}
    </div>
  );
};

export default ReservationList;
