-- ========================================
-- V2__fix_admin_password.sql
-- Sistema de Facturación
-- Fecha: 2026-03-30
-- Descripción: Corrige el hash BCrypt del usuario admin.
--              El hash original en V1 no correspondía a 'admin123'.
--              Este hash fue generado y verificado con BCryptPasswordEncoder.
-- ========================================

UPDATE users
SET password = '$2a$10$EJ552/Yi50s2ubZZVEcqqehZwpGSNOUIHle8ubcm.mHRUzWvn6jci'
WHERE username = 'admin';

