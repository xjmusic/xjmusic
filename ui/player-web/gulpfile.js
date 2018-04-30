/* globals require */
const gulp = require('gulp');
const sass = require('gulp-sass');
const minifyCss = require('gulp-minify-css');
const autoprefixer = require('gulp-autoprefixer');
const htmlmin = require('gulp-htmlmin');
const uglify = require('gulp-uglify');
const babel = require('gulp-babel');
const browserSync = require('browser-sync').create();
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
    }
};

gulp.task('sass', () => {
    pump([
        gulp.src(`${srcPath}/scss/**/*.scss`),
        plumber(err => console.error(err)),
        sass({style: 'compressed'}).on('error', sass.logError),
        autoprefixer({browsers: ['last 2 versions']}),
        minifyCss(),
        gulp.dest(`${distPath}/css`),
        browserSync.stream({match: '**/*.css'})
    ]);
});

gulp.task('html', () => {
    pump([
        gulp.src(`${srcPath}/**/*.html`),
        plumber(err => console.error(err)),
        htmlmin({collapseWhitespace: true, removeComments: true}),
        gulp.dest(`${distPath}`)
    ]);
});

gulp.task('js', () => {
    pump([
        gulp.src(`${srcPath}/js/**/*.js`),
        plumber(err => console.error(err)),
        babel(config.babel),
        uglify(),
        gulp.dest(`${distPath}/img`),
        browserSync.stream()
    ]);
});

gulp.task('vendor', () => {
    pump([
        gulp.src([
            `${srcPath}/vendor/*`
        ]),
        plumber(err => console.error(err)),
        gulp.dest(`${distPath}/vendor`),
        browserSync.stream()
    ]);
});

gulp.task('image', () => {
    pump([
        gulp.src(`${srcPath}/img/*`),
        plumber(err => console.error(err)),
        imagemin({verbose: true}),
        gulp.dest(`${distPath}/img`)
    ]);
});

gulp.task('browserSync', ['sass', 'js', 'image', 'vendor', 'html'], () => {
    browserSync.init({
        injectChanges: true,
        server: `./${distPath}`,
    });

    gulp.watch(config.src.scss, ['sass']);
    gulp.watch(config.src.js, ['js']);
    gulp.watch(config.src.html, ['html']);
    gulp.watch(config.src.img, ['image']);
    gulp.watch(config.src.html).on('change', browserSync.reload);
    gulp.watch(config.src.js).on('change', browserSync.reload);
});

gulp.task('default', ['browserSync']);
