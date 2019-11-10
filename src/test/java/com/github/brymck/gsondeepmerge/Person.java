package com.github.brymck.gsondeepmerge;

import java.util.List;

class Person {
  private String name;
  private Person spouse;
  private List<Person> children;

  Person() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Person getSpouse() {
    return spouse;
  }

  public void setSpouse(Person spouse) {
    this.spouse = spouse;
  }

  public List<Person> getChildren() {
    return children;
  }

  public void setChildren(List<Person> children) {
    this.children = children;
  }
}
