/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

export const selectStyles = {
  input: (provided, state) => ({
    ...provided,
    color: '#fff',
  }),
  menu: (provided, state) => ({
    ...provided,
    color: '#fff',
    background: '#191919',
    margin: 0,
    border: 0,
    borderRadius: 0,
    webkitBorderRadius: 0,
  }),
  menuList: (provided, state) => ({
    ...provided,
    color: '#fff',
    background: '#191919',
    margin: 0,
    border: 0,
    borderRadius: 0,
    webkitBorderRadius: 0,
  }),
  singleValue: (provided, state) => ({
    ...provided,
    color: '#fff',
    padding: 0,
    margin: 0,
  }),
  control: (provided, state) => ({
    ...provided,
    background: '#292929',
    border: 0,
    borderRadius: 0,
    webkitBorderRadius: 0,
    minHeight: 0,
  }),
  indicatorsContainer: (provided, state) => ({
    ...provided,
    padding: 0,
    margin: 0,
  }),
  indicatorContainer: (provided, state) => ({
    ...provided,
    padding: '0 !important',
    margin: 0,
  }),
  indicatorSeparator: (provided, state) => ({
    ...provided,
    padding: '0 !important',
    backgroundColor: '#444',
    margin: 0,
  }),
  container: (provided, state) => ({
    ...provided,
    width: '200px',
  }),
  option: (provided, state) => ({
    ...provided,
    color: state.isSelected ? 'white' : '#666',
    padding: 4,
    marginTop: 0,
  }),
};

export const selectTheme = (theme) => ({
  ...theme,
  borderRadius: 0,
  colors: {
    ...theme.colors,
    text: '#fff',
    primary: '#636',
  },
  spacing: {
    baseUnit: 2,
    controlHeight: 0,
    menuGutter: 0,
  }
});

/*
export const selectTheme = {
  borderRadius: 0,
  colors: {},
};
*/
