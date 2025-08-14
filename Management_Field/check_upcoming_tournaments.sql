SELECT tournament_id, name, start_date, end_date, status FROM tournaments WHERE start_date > GETDATE() AND status = 'ACTIVE' ORDER BY start_date ASC;
