declare module 'bpmn-js/lib/Modeler' {
  import Modeler from 'bpmn-js/lib/Modeler';
  export default Modeler;
}

declare module 'cmmn-js/lib/Modeler' {
  const CmmnModeler: {
    new(options: any): any;
    prototype: any;
  };
  export default CmmnModeler;
}
