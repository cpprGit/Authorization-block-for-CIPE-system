import {createContext, useContext} from 'react';
import {CppwApi} from 'src/api/api';

export const CppwApiContext = createContext<CppwApi | undefined>(undefined);

export const useCppwApiContext = () => useContext(CppwApiContext);
