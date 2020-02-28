/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import React from 'react'
import { render } from '@testing-library/react'
import App from './App'

test('renders the base application', () => {
  const { getByText } = render(<App />);
  // TODO test a URL inside /mk3 to avoid warning during test
  // TODO test something const linkElement = getByText(/learn react/i);
  // TODO make some assertion expect(linkElement).toBeInTheDocument();
});
