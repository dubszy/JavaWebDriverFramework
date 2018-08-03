package com.mwaltman.podriverframework.common.loadables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This interface represents the relative path to the page object it is annotated on.
 *
 * The purpose of this interface is to give the framework, tests written with it,
 * and the test writer the ability to understand where a page object lies within
 * the page hierarchy.
 *
 * The path excludes the domain name and the path(s) to parent pages. For example,
 * consider two page objects named ParentPage and ChildPage, where ChildPage is
 * fully-encapsulated on the target website by ParentPage and ChildPage extends ParentPage.
 * If the path to ChildPage on the target website is http://www.example.com/parent/child,
 * the @Path annotation's value for ParentPage is "/parent" and the value for ChildPage
 * is "/child".
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    String value() default "";
}
