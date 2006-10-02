package tesis.extensiones;

/**
 * See page 276 of DESIGN PATTERNS [1995].
 * Implemented by Blueprint Technologies, Inc.
 */

/**
 * Package
 */

//package com.blueprint.patterns.gamma.behavioral.mediator;

/**
 * Implements cooperative behavior by coordinating
 * Colleague objects. Knows and maintains its colleagues.
 */

public class ConcreteMediator implements Mediator
{
	private ConcreteColleague1 colleague1 = new ConcreteColleague1();
	private ConcreteColleague2 colleague2 = new ConcreteColleague2();
}