var gulp = require('gulp');
var gutil = require('gulp-util');
var sass = require('gulp-sass');
var cssnano = require('gulp-cssnano');
var rename = require('gulp-rename');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');

gulp.task('fonts', function() {
    gulp.src('./node_modules/bootstrap-sass/assets/fonts/bootstrap/*')
        .pipe(gulp.dest('./static/fonts'));
});

gulp.task('css', function() {
    return gulp.src('./static/css/theme.scss')
        .pipe(sass().on('error', handleError))
        .pipe(gulp.dest('./static/css'))
        .pipe(cssnano())
        .pipe(rename({ suffix: '.min' }))
        .pipe(gulp.dest('./static/css'));
});

gulp.task('js', function() {
    return gulp.src('./static/js/!(theme.js|theme.min.js)')
        .pipe(concat('theme.js'))
        .pipe(gulp.dest('./static/js'))
        .pipe(uglify())
        .pipe(rename({ suffix: '.min' }))
        .pipe(gulp.dest('./static/js'));
});

gulp.task('watch', function() {
    gulp.watch('./static/**/*.scss', ['css']);
    gulp.watch('./static/**/*.js', ['js']);
});

gulp.task('default', function() {
    gulp.start('fonts', 'css', 'js');
});

function handleError(err) {
    gutil.log(err);
    this.emit('end');
}
