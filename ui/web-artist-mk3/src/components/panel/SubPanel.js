/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from "react";
// app
import "./SubPanel.scss"

/**
 SubPanel for framing a context as a subset of a larger context (e.g. pattern selector in voice editor)

 @return {*}
 @param {{
    disabled Boolean,
    ...all others passed through...
 }} props passed through directly to sub-panel component
 */
export default function SubPanel(props) {
  return (
    <div className={`sub-panel ${!!props.disabled ? "disabled" : ""}`}
         {...props}/>
  );
};

