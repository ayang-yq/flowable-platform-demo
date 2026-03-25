declare module 'bpmn-js/lib/Modeler' {
  import Modeler from 'bpmn-js/lib/Modeler';
  export default Modeler;
}

declare module 'cmmn-js/lib/Modeler' {
  interface CmmnModeler {
    new(options: any): any;
    prototype: any;
  }
  const CmmnModeler: CmmnModeler;
  export = CmmnModeler;
}
