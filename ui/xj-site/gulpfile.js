// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

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

gulp.task('watch', ['default'], function () {
    gulp.watch('themes/xj-theme/sass/**/*.scss', ['build-css']);
    gulp.watch('images/**/*.jpg', ['resize-images']);
});

gulp.task('resize-images', function () {
    resizeImages(1280, 0.5, '-large');
    resizeImages(480, 0.3, '-small');
});

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

// Default Task
gulp.task('default', ['resize-images', 'build-css']);
