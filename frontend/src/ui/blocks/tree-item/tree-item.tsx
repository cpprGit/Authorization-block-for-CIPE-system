import {Icon, Intent, ITreeNode, Spinner, Tree} from '@blueprintjs/core';
import React, {FC, memo} from 'react';
import {ProfileOrSearchItem} from 'src/types';
import {useCppwApiContext} from 'src/ui/contexts/cppw-api-context/cppw-api-context';
import './tree-item.styl';

const SPINNER_ITEM: ITreeNode = {
    id: 'spinner',
    label: <Spinner size={15} />,
};

const ERROR_ITEM: ITreeNode = {
    id: 'error',
    label: (
        <span className='tree-item__error-view'>
            <Icon icon={'error'} intent={Intent.DANGER} /> Ошибка загрузки списка
        </span>
    ),
};

type Props = {
    items: ITreeNode[];
    editItem: (newItems: ITreeNode[]) => void;
};
export const TreeItem: FC<Props> = memo(({items, editItem}) => {
    const cppwApi = useCppwApiContext();
    const handleNodeCollapse = (nodeData: ITreeNode) => {
        nodeData.isExpanded = false;
        editItem([...items]);
    };
    const handleNodeExpand = (nodeData: ITreeNode) => {
        if (nodeData.hasCaret && !nodeData.childNodes) {
            cppwApi &&
                cppwApi.getOrganisationStructure(
                    String(nodeData.id),
                    () => {
                        nodeData.isExpanded = true;
                        nodeData.childNodes = [SPINNER_ITEM];
                        editItem([...items]);
                    },
                    (result: ProfileOrSearchItem[]) => {
                        nodeData.childNodes = result[0].items;
                        editItem([...items]);
                    },
                    () => {
                        nodeData.isExpanded = true;
                        nodeData.childNodes = [ERROR_ITEM];
                        editItem([...items]);
                    }
                );
        } else {
            nodeData.isExpanded = true;
            editItem([...items]);
        }
    };
    return (
        <>
            <Tree
                contents={items}
                onNodeCollapse={handleNodeCollapse}
                onNodeExpand={handleNodeExpand}
            />
            {(!items || !items.length) && (
                <div className='profile-lists__no-items'>Список пока что пуст</div>
            )}
        </>
    );
});
