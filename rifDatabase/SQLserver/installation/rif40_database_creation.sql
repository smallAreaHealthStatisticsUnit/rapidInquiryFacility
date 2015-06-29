/*
Creation of sahsuland_dev database
*/

IF EXISTS(SELECT * FROM sys.sysdatabases where name='sahsuland_dev')
	DROP DATABASE sahsuland_dev;
	
CREATE DATABASE sahsuland_dev;


USE [sahsuland_dev];
create login rif40 WITH PASSWORD='xxxxx';
CREATE USER [rif40] FOR LOGIN [rif40] WITH DEFAULT_SCHEMA=[dbo];
CREATE SCHEMA [rif40] AUTHORIZATION [rif40];
CREATE SCHEMA [rif_data] AUTHORIZATION [rif40];
