-- --------------------------------------------------
-- Rename Table `account_user` to `account_user_role`
-- --------------------------------------------------
RENAME TABLE `account_user` TO `account_user_role`;
--
-- Table already has `type` column, which now refers to a Role Type.
--
