let totalBars = 64;
let totalCycles = 1;
let totalSeconds = 4;

let barWidth = 4;
let barMarginX = 6;
let barMaxHeight = 15;
let barFillColor = '#555';

let canvasWidth = totalBars * barWidth + (totalBars - 1) * barMarginX;
let canvasHeight = barMaxHeight * 2;

console.log('<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="' + canvasWidth + 'px" height="' + canvasHeight + 'px" viewBox="0 0 ' + canvasWidth + ' ' + canvasHeight + '" style="enable-background:new 0 0 ' + canvasWidth + ' ' + canvasHeight + ';" xml:space="preserve">');

for (let i = 0; i < totalBars; i++) {
  let barX = null;
  let barY = null;
  let barHeight = null;

  console.log('  ' + '<rect x="' + barX + '" y="' + barY + '" width="' + barWidth + '" height="' + barHeight + '" fill="' + barFillColor + '">' + '\n' +
    '    ' + '<animate attributeName="height" attributeType="XML" values="5;21;5" begin="0s" dur="1.8s" repeatCount="indefinite"></animate>' + '\n' +
    '    ' + '<animate attributeName="y" attributeType="XML" values="13; 5; 13" begin="0s" dur="1.8s" repeatCount="indefinite"></animate>' + '\n' +
    '  ' + '</rect>' + '\n');
}

console.log('</svg>');
