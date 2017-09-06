export function initialize() {
  const application = arguments[1] || arguments[0];
  application.inject('component:route-headline', 'applicationRoute', 'route:application');
}

export default {
  name: 'route-headline',
  initialize
};
