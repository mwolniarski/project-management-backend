CREATE OR REPLACE FUNCTION createNotificationWhenTaskIsDeleted()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO notifications (status, notification_content, related_to_id)
VALUES ('UNREAD', 'Task ' || OLD.name || ' was deleted', OLD.task_owner_id);
RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER createNotificationOnDelete
    AFTER DELETE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION createNotificationWhenTaskIsDeleted();


CREATE OR REPLACE FUNCTION generateTaskRaport(status_filter text, date_from date, date_to date, org_id int)
RETURNS TABLE (id int, name varchar(30), due_date date, status_type varchar(15)) AS $$
BEGIN
RETURN QUERY
SELECT t.id, t.name, t.due_date, t.status
FROM tasks t
inner join task_groups tg on t.task_group_id = tg.id
inner join projects p on tg.project_id = p.id
WHERE (status_filter IS NULL OR t.status = status_filter)
  AND (date_from IS NULL OR t.due_date >= date_from)
  AND (date_to IS NULL OR t.due_date <= date_to)
  AND (project_id IS NULL OR p.organization_org_id = org_id);
END;
$$ LANGUAGE plpgsql;