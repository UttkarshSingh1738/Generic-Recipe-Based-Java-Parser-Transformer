declare module "@babel/template" {
  function template(code: string, opts?: object): (nodes?: object) => unknown;
  namespace template {
    function ast(code: string, opts?: object): unknown;
  }
  export default template;
}
