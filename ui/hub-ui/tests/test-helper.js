//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import resolver from './helpers/resolver';
import {setResolver} from 'ember-mocha';
import './helpers/flash-message';

import Application from '../app';
import config from '../config/environment';
import {setApplication} from '@ember/test-helpers';

setResolver(resolver);

setApplication(Application.create(config.APP));
