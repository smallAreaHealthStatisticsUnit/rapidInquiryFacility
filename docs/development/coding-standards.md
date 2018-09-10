---
layout: default
title: Coding Standards for the Rapid Inquiry Facility (RIF)
---

These are the standards -- or at least guidelines -- that we aim to use for RIF code.

## Java

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with a couple of minor differences:

- indentation is with tabs, not spaces;
- there is lots of old code that does not obey this guide.

On that second point, tidying up as well as improving old code as you touch it is encouraged.

### Boilerplate

Many files still contain huge chunks of unnecessary boilerplate comments, firstly reiterating the licence, and then having a lot of nonsense about code organisation. Feel free to remove these as you find them. Just keep anything that actually provides information.

## R

There don’t seem to be any particular standards for R coding. It’s a bit of a lawless landscape. At least try to keep the line length under 100, as per Java. Use meaningful names, and assign values with the `<-` operator, rather than `=`, as seems to be the standard recommendation. Why there are two ways to assign a value, I can’t even.

## JavaScript

[Google has one for JS as well](https://google.github.io/styleguide/jsguide.html), so maybe try to follow that. But with tabs.

## HTML and CSS

Just don’t do anything too ugly.
