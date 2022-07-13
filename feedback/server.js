// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

const express = require('express');
const app = express();

app.post('/v2', (req, res) => {
  res.send('OK');
});

// Listen to the App Engine-specified port, or 8080 otherwise
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`Server listening on port ${PORT}...`);
});
