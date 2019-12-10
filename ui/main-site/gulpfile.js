// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

let gulp = require('gulp'),
  imageResize = require('gulp-image-resize'),
  imageMin = require('gulp-imagemin'),
  rename = require('gulp-rename'),
  sass = require('gulp-sass');

let sassConfig = {
  inputDirectory: 'themes/xj-theme/sass/**/*.scss',
  outputDirectory: 'themes/xj-theme/static/css',
  options: {
    outputStyle: 'condensed'
  }
};

gulp.task('build-css', function () {
  return gulp
    .src(sassConfig.inputDirectory)
    .pipe(sass(sassConfig.options).on('error', sass.logError))
    .pipe(gulp.dest(sassConfig.outputDirectory));
});

gulp.task('resize-images', function (done) {
  resizeImages(1280, 0.5, '-large');
  resizeImages(480, 0.3, '-small');
  done();
});

gulp.task('default', gulp.series('resize-images', 'build-css'));

gulp.task('watch', gulp.series('default', function () {
  gulp.watch('themes/xj-theme/sass/**/*.scss', gulp.series('build-css'));
  gulp.watch('images/**/*.jpg', gulp.series('resize-images'));
}));

/**
 .jpg files only!
 * @param width
 * @param quality
 * @param suffix
 */
function resizeImages(width, quality, suffix) {
  gulp.src('images/**/*.jpg')
    .pipe(rename({
      suffix: suffix
    }))
    .pipe(imageResize({
      quality: quality,
      imageMagick: true,
      width: width,
      height: "",
      upscale: false
    }))
    .pipe(imageMin([
      imageMin.jpegtran({progressive: true})
    ]))
    .pipe(gulp.dest('static/images'));
}
