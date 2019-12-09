-- --------------------------------------------------
-- Rename Table `account_user` to `account_user_role`
-- --------------------------------------------------
RENAME TABLE `account_user_role` TO `account_user`;

# Get rid of type column
ALTER TABLE `account_user` DROP COLUMN `type`;
