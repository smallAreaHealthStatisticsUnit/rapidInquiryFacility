---
layout: default
title: The use of Angular.js is discussed in RIF Front End, April 2019
---

1. Contents
{:toc}

# Introduction

Assumes you are familiar with Angular nomenclature, so you know a partial from a directive.

# General Layout of the RIF files in *rifWebApplication\src\main\resources*:

* backend/services
* css: stylesheets
* dashboards: core dashboard modals, divided into export, login, mapping, submission and viewer
  and then sub divided into: controllers, directives, partials and services
* images: images used by the RIF, divided into: colorBrewer, glyphicon and trees
* libs: libraries used by the RIF. Generally browserified code is is standalone and other libraries 
  including RIF modified ones are in the root (libs)
* modules: The RIF Angular module
* utils: common utilities, divided into controllers, directives, partials and services

## Naming Convention

*rif[c/p/d/s]-&lt;specific dashboard or utility&gt;-&lt;name&gt;* E.g:

* rifp-dsub-main.html: partial for the main submission screen (with the four trees)
* rifc-dsub-main.js: controller for the main submission screen

They will be found in dashboards/submission.

The abbreviations [c/p/d/s] are:

* **c**: controller;
* **d**: directive;
* **p**: partial;
* **s**: service.

The specific dashboard or utility is:

* **dsub: study submission dashboard in in *dashboards/submission*;
* **expt: study export dashboard;
* **login**: login popup modal;
* **dmap**: dual map mapping dashboard;
* **view**: map and data viewer dashboard;
* **util**: utilities;

# Libraries

# Dashboards

## Login
## Submission
## Viewer
## Mapping
## Export

# Utilities


**Peter Hambly, April 2019**