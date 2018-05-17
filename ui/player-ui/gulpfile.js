// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

/* globals require */
const gulp = require('gulp');
const sass = require('gulp-sass');
const minifyCss = require('gulp-minify-css');
const autoprefixer = require('gulp-autoprefixer');
const htmlmin = require('gulp-htmlmin');
const uglify = require('gulp-uglify');
const babel = require('gulp-babel');
// const browserSync = require('browser-sync').create();
const imagemin = require('gulp-imagemin');
const plumber = require('gulp-plumber');
const pump = require('pump');

// ECMAScript 6 transmogrification configuration
const srcPath = 'src',
  distPath = 'dist';

// Configuration
const config = {
  babel: {
    presets: ['es2015', 'es2017'],
    plugins: ["transform-class-properties"]
  },
  src: {
    html: `${srcPath}/**/*.html`,
    img: `${srcPath}/img/*`,
    js: `${srcPath}/js/**/*.js`,
    sass: `${srcPath}/sass/**/*.scss`,
    vendor: `${srcPath}/vendor/*`,
  },
  dist: {
    base: `./${distPath}`,
    css: `${distPath}/css`,
    img: `${distPath}/img`,
    js: `${distPath}/js`,
    vendor: `${distPath}/vendor`,
  }
};

gulp.task('sass', () => {
  pump([
    gulp.src(config.src.sass),
    plumber(err => console.error(err)),
    sass({style: 'compressed'}).on('error', sass.logError),
    autoprefixer({browsers: ['last 2 versions']}),
    minifyCss(),
    gulp.dest(config.dist.css)
    // browserSync.stream({match: '**/*.css'})
  ]);
});

gulp.task('html', () => {
  pump([
    gulp.src(config.src.html),
    plumber(err => console.error(err)),
    htmlmin({collapseWhitespace: true, removeComments: true}),
    gulp.dest(config.dist.base)
  ]);
});

gulp.task('js', () => {
  pump([
    gulp.src(config.src.js),
    plumber(err => console.error(err)),
    babel(config.babel),
    uglify(),
    gulp.dest(config.dist.js)
    // browserSync.stream()
  ]);
});

gulp.task('vendor', () => {
  pump([
    gulp.src([
      config.src.vendor
    ]),
    plumber(err => console.error(err)),
    gulp.dest(config.dist.vendor)
    // browserSync.stream()
  ]);
});

gulp.task('image', () => {
  pump([
    gulp.src(config.src.img),
    plumber(err => console.error(err)),
    imagemin({verbose: true}),
    gulp.dest(config.dist.img)
  ]);
});

gulp.task('watch', ['sass', 'js', 'image', 'vendor', 'html'], () => {

/*
  // Not useful to launch http://localhost:3000/ because XJ Player is developed via Docker and docker-compose
  browserSync.init({
    injectChanges: true,
    server: config.dist.base,
  });
*/

  gulp.watch(config.src.sass, ['sass']);
  gulp.watch(config.src.js, ['js']);
  gulp.watch(config.src.html, ['html']);
  gulp.watch(config.src.img, ['image']);
  // gulp.watch(config.src.html).on('change', browserSync.reload);
  // gulp.watch(config.src.js).on('change', browserSync.reload);
});

gulp.task('default', ['sass', 'js', 'image', 'vendor', 'html']);

