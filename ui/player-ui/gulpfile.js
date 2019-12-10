// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

/* globals require */
const gulp = require('gulp');
const sass = require('gulp-sass');
let cleanCSS = require('gulp-clean-css');
 const autoprefixer = require('gulp-autoprefixer');
const htmlmin = require('gulp-htmlmin');
const uglify = require('gulp-uglify');
const babel = require('gulp-babel');
// const browserSync = require('browser-sync').of();
const imagemin = require('gulp-imagemin');
const plumber = require('gulp-plumber');
const pump = require('pump');
const filter = require('gulp-filter');

// ECMAScript 6 transmogrification configuration
const srcPath = 'src',
  distPath = 'dist',
  npmPath = 'node_modules';

// Configuration
const config = {
  babel: {
    presets: ['@babel/env'],
    plugins: ['@babel/plugin-proposal-class-properties']
  },
  src: {
    html: `${srcPath}/**/*.html`,
    img: `${srcPath}/img/*`,
    js: `${srcPath}/js/**/*.js`,
    sass: `${srcPath}/sass/**/*.scss`,
    vendArr: [`${srcPath}/vendor/*`],
  },
  dist: {
    base: `./${distPath}`,
    css: `${distPath}/css`,
    img: `${distPath}/img`,
    js: `${distPath}/js`,
    vendor: `${distPath}/vendor`,
  }
};

gulp.task('sass', (done) => {
  pump([
    gulp.src(config.src.sass),
    plumber(err => console.error(err)),
    sass({style: 'compressed'}).on('error', sass.logError),
    autoprefixer({browsers: ['last 2 versions']}),
    cleanCSS(),
    gulp.dest(config.dist.css)
    // browserSync.stream({match: '**/*.css'})
  ]);
  done();
});

gulp.task('html', (done) => {
  pump([
    gulp.src(config.src.html),
    plumber(err => console.error(err)),
    htmlmin({collapseWhitespace: true, removeComments: true}),
    gulp.dest(config.dist.base)
  ]);
  done();
});

gulp.task('js', (done) => {
  pump([
    gulp.src(config.src.js),
    filter(['**', '!**/__mocks__/**', '!**/__tests__/**']),
    plumber(err => console.error(err)),
    babel(config.babel),
    uglify(),
    gulp.dest(config.dist.js)
    // browserSync.stream()
  ]);
  done();
});

gulp.task('vendor', (done) => {
  pump([
    gulp.src(config.src.vendArr),
    plumber(err => console.error(err)),
    gulp.dest(config.dist.vendor)
    // browserSync.stream()
  ]);
  done();
});

gulp.task('image', (done) => {
  pump([
    gulp.src(config.src.img),
    plumber(err => console.error(err)),
    imagemin({verbose: true}),
    gulp.dest(config.dist.img)
  ]);
  done();
});

gulp.task('watch', gulp.series('sass', 'js', 'image', 'vendor', 'html', function(){

  /*
    // Not useful to launch http://localhost:3000/ because XJ Player is developed via Docker and docker-compose
    browserSync.init({
      injectChanges: true,
      server: config.dist.base,
    });
  */

  gulp.watch(config.src.sass, gulp.series('sass'));
  gulp.watch(config.src.js, gulp.series('js'));
  gulp.watch(config.src.html, gulp.series('html'));
  gulp.watch(config.src.img, gulp.series('image'));
  // gulp.watch(config.src.html).on('change', browserSync.reload);
  // gulp.watch(config.src.js).on('change', browserSync.reload);
}));

gulp.task('default', gulp.series(['sass', 'js', 'image', 'vendor', 'html']));

