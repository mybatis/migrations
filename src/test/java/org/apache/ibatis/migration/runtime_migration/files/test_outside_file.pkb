CREATE OR REPLACE PACKAGE BODY test_outide_file
BEGIN
  PROCEDURE do_nothing
  IS
    BEGIN
      SELECT * FROM dual;
    END;
END;