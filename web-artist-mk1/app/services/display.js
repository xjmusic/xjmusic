/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {htmlSafe} from '@ember/string';
import Service, {inject as service} from '@ember/service';

export default Service.extend({

  flashMessages: service(),

  error(errorData) {
    this.flashMessages.danger(this.renderErrorData(errorData));
  },

  renderErrorData(errorData) {
    if ('errors' in errorData && errorData.errors.length >= 1)
      return this.renderError(errorData.errors[0]);

    return `Error: ${errorData.toString()}`;
  },

  renderError(error) {
    console.error("Error!", error);
    if ('detail' in error && error.detail)
      return htmlSafe(`<h6>${error.title}</h6><p>${error.detail}</p>`);
    else
      return error.title;
  },

  success(message) {
    this.flashMessages.success(message);
  },

  warning(message) {
    this.flashMessages.warning(message);
  }

});
