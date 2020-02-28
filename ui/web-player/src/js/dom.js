/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

/**
 * DOM manipulation utility
 */
export class DOM {

  /**
   Add a Class to a DOM element

   @param {Element} element to which a class will be added
   @param {String} name of class to add
   */
  static addClass(element, name) {
    let arr = element.className.split(" ");
    if (arr.indexOf(name) === -1) {
      element.className += " " + name;
    }
  }

  /**
   Remove a Class to a DOM element

   @param {Element} element from which a class will be removed
   @param {String} name of class to remove
   */
  static removeClass(element, name) {
    element.className = element.className.replace(`/\b${name}\b/g`, "");
  }

  /**
   Set the HTML content of a DOM element

   @param {Element} element to clear and set new HTML content of
   @param {String} html content to set
   */
  static setHTML(element, html) {
    element.innerHTML = html;
  }

  /**
   Prepend HTML content inside a DOM element

   @param {Element} element within which to prepend HTML content
   @param {Element} innerEl content to prepend
   */
  static prepend(element, innerEl) {
    element.insertBefore(innerEl, element.firstChild);
  }

  /**
   Hide DOM elements

   @param {Element} element element or array of elements to hide
   */
  static hide(element) {
    element['style']['display'] = "none";
  }
}
